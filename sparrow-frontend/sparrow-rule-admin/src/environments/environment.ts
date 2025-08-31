
// const SPARROW_BASE='https://api2.linkair-tech.cn/sparrow-permission-service'
const SPARROW_BASE='http://localhost:8999'
// const SPARROW_API='https://api2.linkair-tech.cn/sparrow-permission-service'

export const environment = {
  production: false,
  // apiBase: 'https://api2.linkair-tech.cn/sparrow-service',
  ruleApi: `http://localhost:8999`,
  jpaBase: `${SPARROW_BASE}/common-jpa-controller`,
  apiBase: `${SPARROW_BASE}`,
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'chnplc',
    clientId: 'chnplc-web',
    login: "check-sso"
  },
};
