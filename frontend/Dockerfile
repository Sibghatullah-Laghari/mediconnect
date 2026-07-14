# Build the frontend application
FROM node:20-alpine AS build
WORKDIR /app

# Install project dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source files and create the production build
COPY . .
RUN npm run build

# Serve the built frontend with Nginx
FROM nginx:1.27-alpine
COPY default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

# Start the Nginx server
CMD ["nginx", "-g", "daemon off;"]
