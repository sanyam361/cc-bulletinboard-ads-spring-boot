# Feature Flags
Based on Spring Cloud Config (as explained [here](Configuration.md)) Feature Flags can be used to enable/disable certain features in the code during runtime. In our example project, we check a user's privileges when creating a new advertisement. Using the code snippets shown below, this check can be enabled/disabled based on a feature flag (which itself can be set using a configuration variable).

You can see an example of this in the tests `createWithUserCheck` and `createWithoutUserCheck` in [AdvertisementControllerTest.java](../src/test/java/com/sap/bulletinboard/ads/controllers/AdvertisementControllerTest.java).

## Further Material
Food 4 Thoughts Session: [Slides](https://jam4.sapjam.com/profile/2FgBeBjbyy7ZVpUjuiXlcD/documents/495ZpQTY3XN7PPtRGp1UQt) [Video](https://jam4.sapjam.com/profile/2FgBeBjbyy7ZVpUjuiXlcD/documents/nkDFEa2FgFTw6RLpfno1QO)

[Learning material](https://github.wdf.sap.corp/cc-devops-course/coursematerial/blob/master/DevOps/Feature_Toggles/README.md) from CD & Devops Course

