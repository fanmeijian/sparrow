
import {KeycloakOnLoad} from 'keycloak-js'
const API_BASE='https://api2.linkair-tech.cn/dengbo-service'
const PEM_BASE='https://api2.linkair-tech.cn/sparrow-permission-service'
const login:KeycloakOnLoad='login-required'

export const environment = {
  production: true,
  apiBase: `${API_BASE}`,
  pemBase: `${PEM_BASE}`,
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'dengbo',
    clientId: 'dengbo-app-admin',
    login: login,
  },
};
