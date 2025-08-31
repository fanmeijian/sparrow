#!/bin/sh

# Replace placeholders in the environment.js (or .ts) with environment variables
sed -i "s|REPLACE_KEYCLOAK_BASE|$KEYCLOAK_API_BASE|g" /usr/share/nginx/html/main.*.js
sed -i "s|REPLACE_KEYCLOAK_REALM|$REALM|g" /usr/share/nginx/html/main.*.js
sed -i "s|REPLACE_KEYCLOAK_CLIENT_ID|$CLIENT_ID|g" /usr/share/nginx/html/main.*.js
sed -i "s|REPLACE_API_BASE|$API_BASE|g" /usr/share/nginx/html/main.*.js
sed -i "s|REPLACE_PEM_BASE|$PEM_BASE|g" /usr/share/nginx/html/main.*.js


# Start Nginx
nginx -g 'daemon off;'
