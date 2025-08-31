import { KeycloakOnLoad } from "keycloak-js";

// const SPARROW_BASE='https://api2.linkair-tech.cn/sparrow-permission-service'
// const SPARROW_BASE = 'http://localhost:8080'
const SPARROW_BASE = 'http://localhost:8300/toupiao-service'
// const API_BASE = 'http://localhost:8081/dengbo-service'
// const API_BASE = 'http://localhost:8300/toupiao-service'
const API_BASE = 'http://localhost:8999'
const login: KeycloakOnLoad = 'login-required'
export const environment = {
  production: false,
  apiBase: `${API_BASE}`,
  pemBase: `${SPARROW_BASE}`,
  // keycloak: {
  //   authServerUrl: 'https://keycloak.linkair-tech.cn',
  //   realm: 'liyun-prd',
  //   clientId: 'toupiao-web',
  //   login: login
  // },
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'chnplc',
    clientId: 'chnplc-web',
    login: login
  },
};
