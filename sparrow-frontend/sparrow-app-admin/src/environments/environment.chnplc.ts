import { KeycloakOnLoad } from "keycloak-js";

const SPARROW_BASE = 'https://api.cn-plc.com/chnplc-service'
const API_BASE = 'https://api.cn-plc.com/chnplc-service'
const login: KeycloakOnLoad = 'login-required'
export const environment = {
  production: true,
  apiBase: `${API_BASE}`,
  pemBase: `${SPARROW_BASE}`,
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'chnplc',
    clientId: 'chnplc-web',
    login: login
  },
};
