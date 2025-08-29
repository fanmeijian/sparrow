const SPARROW_BASE = 'https://api2.linkair-tech.cn/sparrow-permission-service'


export const environment = {
  production: true,
  jpaBase: `${SPARROW_BASE}/common-jpa-controller`,
  apiBase: `${SPARROW_BASE}`,
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'dengbo',
    clientId: 'dengbo-web',
    login: "check-sso"
  },
};
