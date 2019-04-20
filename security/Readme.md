## Disclaimer and Overview
The procedure described in this document is a workaround and therefore intended for internal use - meaning SAP Developers -  only. A final solution, which would then also be available for external customers, needs yet to be created. Please follow [XSTWOSEC-433][1] to stay up to date on the status of the final solution.

The workaround utilizes the [APIs for Authorization Management][2], which are normally only used by the Security UIs of the SAP Cloud Platform Cockpit (SAP CPC). The workaround might need to be changed, if an incompatible change is introduced in one or more of the [Authorization Management APIs][1].

The workaround consists of a set of ```curl``` calls. The set is divided into two sub-groups. The first group is responsible for getting the neccessary authorizations for authorization administration and for maintaining the authorizations of the business application, that is deployed by the Continuous Delivery Pipeline. The calls must be executed in a distinct order as specified in the following:

  - Get API Access-Token from XSUAA
  - Get Role-Templates with AppId
  - Create Role with Role-Template details
  - Create Role-Collection
  - Add Role(s) to Role-Collection
  - Create Role-Collection Mapping

The second group of ```curl``` calls is responsible for tearing down the authorizations of the business application. These calls must also be executed in the following order:

  - Delete Role-Collection Mapping
  - Delete Role-Collection
  - Delete Role

All calls are documented in greater detail further below in this document.

## Prerequisites
The workaround solution requires the following prerequisites:

  - Define a Technical User with Password in the [Standard SAP ID Service][3]  
  - Create a Subscription for the XSUAA Administration Tools
  - Maintain Authorizations for the Technical User

These steps must be performed manually and once, as part of the permanent setup of the CD Pipeline infrastructure.
### Define a Technical User with Password
You can register the technical User with the registration page of website [www.sap.com][4]. The registration page itself has a mandatory field, in which you need to enter a valid e-mail address. The system will send an activation link to the e-mail address you enter here.

In case you do not want to enter the e-mail address of a "real" individual for a technical user, you can create a "fake" e-mail address instead:

  - Go to website [Mailinator][5]. Use the name suggested by Mailinator or use a name made up by yourself to create an e-mail inbox
  - Go to website [TrashMail][6]. For field "New disposable e-mail address", use either the default value or a value of your own. Select a domain from the drop-down list box. Enter the name of your new Mailinator inbox in the field "Your real e-mail address". You can optionally limit the "Number of forwards" to 1.

Now you can enter the e-mail address of [TrashMail][6] into the respective field of the [registration page][4]. Standard SAP ID Service will send the activation link to your TrashMail e-mail account. TrashMail will forward the activation link to your Mailinator e-mail account.

Check your Mailinator inbox and click on the activation link to activate the new SAP ID Service account of the technical user. User ```cc-auth.admin@objectmail.com``` has been created this way: the e-mail address does not exist in reality.
### Create a Subscription for the XSUAA Administration Tools
The API Access-Token is obtained via the OAuth 2 standard flow "User Password Grant". XSUAA will grant the authorizations for administrating authorizations only then if the technical user AND the OAuth 2 Client - which is the Business Application - BOTH have the necessary scopes assigned.

Hence, the scopes which grant "Authorization Administration" must be assigned to the technical user. To do this, we need an already deployed application which already holds a role containing the required scopes (xs_authorization.read and xs_authorization.write).

The "XSUAA Admnistration Tools" is such an application which fulfills that criteria: it is deployed and registered under the AppId "admin-tools!t21". The application already holds a default role "AuthorizationAdmin", which contains the required scopes. In order to be able to reference that role, a subscription to the "XSUAA Administration Tools" is required.

Goto the SAP Jam page [HowTo: Create a Subscription for the UAA Authorization Admin Tool][7] and follow the steps described there. You do not need to do that if you applied the workaround for the issue [SAP CP Cockpit does not contain Menu-Item Security][8], which implicitly creates a subscription for the "XSUAA Administration Tools".
### Maintain Authorizations for the Technical User
Create a new Role-Collection with a name of your choice. Navigate into the new Role-Collection and create an assignment to Role "AuthorizationAdmin" of AppId "admin-tools!t21". Finally, assign the new Role-Collection to the Technical User. If you are not sure how to do this, then refer to __steps 2, 3 and 5__ of the tutorial [HowTo: Administrate Authorizations for CF Applications using the SAP CP Cockpit][9]
## Setup Authorizations automatically
### Extend the Security Descriptor (file ```xs-security.json```) of the Business Application with Role-Template "AuthorizationAdmin"
The API Access-Token is obtained via the OAuth 2 standard flow "User Password Grant". XSUAA will grant the authorizations for administrating authorizations only then if the technical user AND the OAuth 2 Client - which is the Business Application - BOTH have the necessary scopes assigned.

Hence, the scopes which grant "Authorization Administration" must be assigned to the Business Application. This is done by enhancing the authorization model of the Business Application with those scopes, which grant the authority to administrate authorizations. These new scopes must then be referenced with an additional Role-Template "AuthorizationAdmin":

```
.
.
.
    "scopes"        : [
.
.
.
                        { "name"                 : "xs_authorization.read",
                          "description"          : "Audit Authorizations"
                        },
                        { "name"                 : "xs_authorization.write",
                          "description"          : "Administrate Authorizations"
                        }
                      ],
    "role-templates": [
.
.
.
                        { "name"                 : "AuthorizationAdmin",
                          "description"          : "Administrate Authorizations",
                          "scope-references"     : [
                                                     "xs_authorization.read",
                                                     "xs_authorization.write"
                                                   ]
                        }
.
.
.
```

This ensures that the Business Application - which represents the OAuth 2 Client - holds the necessary scopes for authorization administration. The intersection of the scope sets for the OAuth 2 Client and the Technical User will now contain the scopes required for administrating authorizations.

The CD Pipeline has the responsibility to enhance the authorization model of the Business Application in a dedicated step during the setup phase, but latest before the request to retrieve the API Access-Token is sent.

As already stated in the disclaimer above: this is a workaround solution. The final solution must not require changing the authorization model of the Business Application. Goal of the final solution must be that the CD Pipeline does not change/modify any artifact of the - to be deployed - Business Application.
### 1. Get API Access-Token from XSUAA
The values for parameters __"client_id"__ and __"client_secret"__ are derived from the client-credentials string __"xsuaa"__ located in environment variable __"VCAP_SERVICES"__. This means that the request can earliest be issued when the Business Application has been deployed to Cloud Foundry and bound to the XSUAA service instance. The deployment of the Business Application can earliest happen after the authorization model of the Business Application has been automatically enhanced by the CD Pipeline, as described in the previous section.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/oauth/token' -i -X POST \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -H 'Accept: application/json' \
    -d 'client_id=<theClientId>&client_secret=<theClientSecret>&grant_type=password&username=cc-auth.admin@objectmail.com&password=<theUserPassword>&token_format=opaque&response_type=token'
```
### 2. Get Role-Templates with AppId
The value for parameter __"AppId"__ is derived from the client-credentials string __"xsuaa"__ located in environment variable __"VCAP_SERVICES"__. The __API Access-Token__ is taken from the response of request no. 1.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/apps/<theAppId>/roletemplates' -i \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
```
### 3. Create Role with Role-Template Details
The value for parameter __"AppId"__ is derived from the client-credentials string __"xsuaa"__ located in environment variable __"VCAP_SERVICES"__. The __API Access-Token__ is taken from the response of request no. 1.

The value(s) for parameter __"roleTemplateName"__ is/are taken from the Role-Templates returned in the response of request no. 2. The values for parameters __"name"__ and __"description"__ can either be generated or derived from a machine-readable decision-matrix. 
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/apps/<theAppId>/roletemplates/<theRoleTemplate>/roles/<nameOfNewRole>' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
    -d '{
          "roleTemplateName":"<theRoleTemplate>",
          "roleTemplateAppId":"<theAppId>",
          "name":"<nameOfNewRole>",
          "description":"<descriptionOfNewRole>"
       }'
```
### 4. Create Role-Collection
The __API Access-Token__ is taken from the response of request no. 1. The values for parameters __"nameOfNewRoleCollection"__ and __"description"__ can either be generated or derived from a machine-readable decision-matrix.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/rolecollections/<nameOfNewRoleCollection>?description=<descriptionOfNewRoleCollection>' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
```
### 5. Add Role(s) to Role-Collection
The value for parameter __"AppId"__ is derived from the client-credentials string __"xsuaa"__ located in environment variable __"VCAP_SERVICES"__. The __API Access-Token__ is taken from the response of request no. 1.

The value(s) for parameter __"roleTemplateName"__ is/are taken from the Role-Templates returned in the response of request no. 2. The values for parameters __"name"__ and __"description"__ are taken from the set of created roles in step no. 3. 

The values for parameter __"nameOfNewRoleCollection"__ are taken from the set of created role-collections in step no. 4.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/rolecollections/<nameOfNewRoleCollection>/roles' -i -X PUT \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
    -d '{
          "roleTemplateName":"<theRoleTemplate>",
          "roleTemplateAppId":"<theAppId>",
          "name":"<nameOfNewRole>",
          "description":"<descriptionOfNewRole>"
       }'
```
### 6. Create Role-Collection Mapping
The value for parameter __"nameOfIdP"__ will usually be __"xsuaa-monitoring-idp"__ for the Muenchhausen FakeIdP. The values for parameter __"saml2UserGroup"__ can either be generated or derived from a machine-readable decision-matrix.

The values for parameter __"nameOfNewRoleCollection"__ are taken from the set of created role-collections in step no. 4. The __API Access-Token__ is taken from the response of request no. 1.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/saml-IdP/<nameOfIdP>/saml-attribute/Groups/equals/<saml2UserGroup>/rolecollections/<nameOfNewRoleCollection>' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
```
## Teardown Authorizations automatically
### 7. Delete Role-Collection Mapping
The value for parameter __"nameOfIdP"__ will usually be __"xsuaa-monitoring-idp"__ for the Muenchhausen FakeIdP. The values for parameter __"saml2UserGroup"__ are taken from the values used in step no. 6.

The values for parameter __"nameOfNewRoleCollection"__ are taken from the values used in step no. 6. The __API Access-Token__ is taken from the response of request no. 1.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/saml-IdP/<nameOfIdP>/saml-attribute/Groups/equals/<saml2UserGroup>/rolecollections/<nameOfNewRoleCollection>' -i -X DELETE \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
```
### 8. Delete Role-Collection
The values for parameter __"nameOfNewRoleCollection"__ are taken from the values used in step no. 5. The __API Access-Token__ is taken from the response of request no. 1.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/rolecollections/<nameOfNewRoleCollection>' -i -X DELETE \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
```
### 9. Delete Role
The value for parameter __"AppId"__ is derived from the client-credentials string __"xsuaa"__ located in environment variable __"VCAP_SERVICES"__. The values for parameters __"theRoleTemplate"__ and __"nameOfNewRole"__ are taken from the values used in step no. 3. The __API Access-Token__ is taken from the response of request no. 1.
```
$ curl 'https://<subdomain>.authentication.<cf-domain>/sap/rest/authorization/apps/<theAppId>/roletemplates/<theRoleTemplate>/roles/<nameOfNewRole>' -i -X DELETE \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <theAPIAccessToken>'
```
## Final Remarks

[1]: https://jtrack.wdf.sap.corp/browse/XSTWOSEC-433?filter=-2 "Automated Authorisation Administration for Continuous Delivery"
[2]: https://wiki.wdf.sap.corp/wiki/x/EGhrbQ "APIs for Authorization Management"
[3]: https://accounts.sap.com "Standard SAP ID Service"
[4]: https://accounts.sap.com/ui/public/showRegisterForm?spName=wcms_sapdx_prod_29052019&targetUrl=&sourceUrl= "Register User in Standard SAP ID Service"
[5]: https://www.mailinator.com/ "Mailinator"
[6]: https://trashmail.com/ "TrashMail"
[7]: https://jam4.sapjam.com/wiki/show/fsiAKlkQONtHutSPdr2h9Z "HowTo: Create a Subscription for the UAA Authorization Admin Tool"
[8]: https://wiki.wdf.sap.corp/wiki/pages/viewpage.action?pageId=1933986729 "SAP CP Cockpit does not contain Menu-Item Security"
[9]: https://jam4.sapjam.com/wiki/show/d2dgJlWR9IpwQsLOCmyJj9 "HowTo: Administrate Authorizations for CF Applications using the SAP CP Cockpit"
