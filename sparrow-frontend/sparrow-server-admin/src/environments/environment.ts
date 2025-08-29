// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
// const SPARROW_BASE='https://api2.linkair-tech.cn/sparrow-permission-service'
const SPARROW_BASE='http://localhost:8080'
// const SPARROW_API='https://api2.linkair-tech.cn/sparrow-permission-service'

export const environment = {
  production: false,
  // apiBase: 'https://api2.linkair-tech.cn/sparrow-service',
  ruleApi: `http://localhost:8999`,
  jpaBase: `${SPARROW_BASE}/common-jpa-controller`,
  apiBase: `${SPARROW_BASE}`,
  keycloak: {
    authServerUrl: 'https://keycloak.linkair-tech.cn',
    realm: 'dengbo',
    clientId: 'dengbo-web',
    login: "check-sso"
  },
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
