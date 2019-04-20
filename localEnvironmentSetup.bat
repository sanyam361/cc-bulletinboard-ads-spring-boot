REM This script prepares the current shell's environment variables (not permanently)

REM SET SPRING_PROFILES_ACTIVE=local
SET VCAP_APPLICATION={}
SET VCAP_SERVICES={"xsuaa":[{"credentials":{"clientid":"sb-bulletinboard!t895","clientsecret":"dummy-clientsecret","identityzone":"uaa","identityzoneid":"1e505bb1-2fa9-4d2b-8c15-8c3e6e6279c6","url":"dummy-url","verificationkey":"-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn5dYHyD/nn/Pl+/W8jNGWHDaNItXqPuEk/hiozcPF+9l3qEgpRZrMx5ya7UjGdvihidGFQ9+efgaaqCLbk+bBsbU5L4WoJK+/t1mgWCiKI0koaAGDsztZsd3Anz4LEi2+NVNdupRq0ScHzweEKzqaa/LgtBi5WwyA5DaD33gbytG9hdFJvggzIN9+DSverHSAtqGUHhwHSU4/mL36xSReyqiKDiVyhf/y6V6eiE0USubTEGaWVUANIteiC+8Ags5UF22QoqMo3ttKnEyFTHpGCXSn+AEO0WMLK1pPavAjPaOyf4cVX8b/PzHsfBPDMK/kNKNEaU5lAXo8dLUbRYquQIDAQAB-----END PUBLIC KEY-----","xsappname":"bulletinboard-d012345"},"label":"xsuaa","name":"uaa-bulletinboard","plan":"application","tags":["xsuaa"]}]}

REM Used for dependent service call
SET USER_ROUTE=https://bulletinboard-users-course.cfapps.sap.hana.ondemand.com
