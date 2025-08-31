# Use Nginx base image
FROM nginx:alpine

# Copy the Angular app build files to the Nginx HTML directory
COPY dist/sparrow-app-admin /usr/share/nginx/html

# Copy the environment variable replacement script
COPY set-env.sh /usr/share/nginx/set-env.sh

# Set execute permission on the script
RUN chmod +x /usr/share/nginx/set-env.sh

# Copy the Nginx config file (if you have a custom one)
# COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

# Run the environment script and then start Nginx
CMD ["/bin/sh", "/usr/share/nginx/set-env.sh"]
