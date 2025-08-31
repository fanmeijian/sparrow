
import {KeycloakOnLoad} from 'keycloak-js'
const API_BASE='REPLACE_API_BASE'
const PEM_BASE='REPLACE_PEM_BASE'
const login:KeycloakOnLoad='login-required'

export const environment = {
  production: true,
  apiBase: `${API_BASE}`,
  pemBase: `${PEM_BASE}`,
  keycloak: {
    authServerUrl: 'REPLACE_KEYCLOAK_BASE',
    realm: 'REPLACE_KEYCLOAK_REALM',
    clientId: 'REPLACE_KEYCLOAK_CLIENT_ID',
    login: login,
  },
};
