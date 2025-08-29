export const environment = {
  production: true,
  bpmApi: `https://api2.linkair-tech.cn/dengbo-bpm`,
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'dengbo',
    clientId: 'dengbo-web',
    login: "check-sso"
  },
};
