#!/usr/bin/env bash
echo "Hint: run script with 'source localEnvironmentSetup.sh'"
echo "This script prepares the current shell's environment variables (not permanently)"

export VCAP_APPLICATION='{}'
# Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOiJ0ZXN0Q2xpZW50IiwiZXhwIjoyMTQ3NDgzNjQ3LCJzY29wZSI6WyJidWxsZXRpbmJvYXJkLWQwMTIzNDUuRGlzcGxheSIsImJ1bGxldGluYm9hcmQtZDAxMjM0NS5VcGRhdGUiXSwidXNlcl9uYW1lIjoidXNlciBuYW1lIiwidXNlcl9pZCI6IkQwMTIzNDUiLCJlbWFpbCI6InRlc3RVc2VyQHRlc3RPcmciLCJ6aWQiOiJ1YWEifQ.WUDrcab99In5ufZg8Vu8_25qb7q8uVR2hMDozR8uLwy_u5B-5iUCnlD8TRHY_tqRlqdNO7A4Wu_az8lvZRYt9hGEhREw-EKSU6hf9kimiYZB50Dn_0AiJ-HlUVREbVDLfGuDkyqL9XsaK9qzN4GZSm52znDDpHVy_1GEjqu5L5hjh7pUdVG6gk3TT1_L7KZ8BxPsH94p1O5XqXD62CII4qu9ZZ4RK6RPbpdKIWHlpkpFdRbkQUIHa92YBzR-SfA_Tm3wrQvSjM98SUp1mI4X-fGRfjmu1r5y7r-Uzg3e3hj9F8waTInKgQwcWUShKMgupPKCGAu-5_nAZDMOCDOM6w
export VCAP_SERVICES='{"xsuaa":[{"credentials":{"clientid":"sb-bulletinboard!t895","clientsecret":"dummy-clientsecret","identityzone":"uaa","identityzoneid":"1e505bb1-2fa9-4d2b-8c15-8c3e6e6279c6","url":"dummy-url","verificationkey":"-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn5dYHyD/nn/Pl+/W8jNGWHDaNItXqPuEk/hiozcPF+9l3qEgpRZrMx5ya7UjGdvihidGFQ9+efgaaqCLbk+bBsbU5L4WoJK+/t1mgWCiKI0koaAGDsztZsd3Anz4LEi2+NVNdupRq0ScHzweEKzqaa/LgtBi5WwyA5DaD33gbytG9hdFJvggzIN9+DSverHSAtqGUHhwHSU4/mL36xSReyqiKDiVyhf/y6V6eiE0USubTEGaWVUANIteiC+8Ags5UF22QoqMo3ttKnEyFTHpGCXSn+AEO0WMLK1pPavAjPaOyf4cVX8b/PzHsfBPDMK/kNKNEaU5lAXo8dLUbRYquQIDAQAB-----END PUBLIC KEY-----","xsappname":"bulletinboard-d012345"},"label":"xsuaa","name":"uaa-bulletinboard","plan":"application","tags":["xsuaa"]}]}'

export USER_ROUTE='https://bulletinboard-users-course.cfapps.sap.hana.ondemand.com'

echo \$VCAP_SERVICES=${VCAP_SERVICES}


