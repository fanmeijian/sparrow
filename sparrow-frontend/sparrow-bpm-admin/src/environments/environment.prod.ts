export const environment = {
  production: true,
  bpmApi: `https://api.lylab.cn/dengbo-bpm`,
  keycloak: {
    authServerUrl: 'https://auth.lylab.cn',
    realm: 'dengbo',
    clientId: 'dengbo-web',
    login: "check-sso"
  },
};
