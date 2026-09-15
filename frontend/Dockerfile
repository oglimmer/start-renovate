# Multi-stage build for the Nuxt frontend.
# The runtime is a Nitro node-server (not static nginx) so /api/generate can be
# served dynamically; all other pages are prerendered into .output/public and
# served as static files by the same Nitro process.
FROM node:24.20.0-alpine@sha256:e67514e5d0f6c46656005e1b693b2ec9d52e80b641307de684d4a015ba7a4eaf AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build for production
RUN npm run build

# Runtime stage: Nitro node-server
FROM node:24.20.0-alpine@sha256:e67514e5d0f6c46656005e1b693b2ec9d52e80b641307de684d4a015ba7a4eaf

WORKDIR /app

# The standalone .output bundle is fully self-contained (Nitro bundles all server
# deps) — no package.json/node_modules install needed at runtime.
COPY --from=build /app/.output ./.output

# Listen on 8080 (matches the k8s Service targetPort and the old nginx port) on
# all interfaces so the ingress/Service can reach it.
ENV NODE_ENV=production \
    NITRO_PORT=8080 \
    NITRO_HOST=0.0.0.0 \
    PORT=8080

# Run as the built-in non-root `node` user (uid 1000) — matches the k8s
# securityContext runAsUser: 1000.
USER node

EXPOSE 8080

# Health check (wget ships in the node:alpine base image).
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

CMD ["node", ".output/server/index.mjs"]
