# Start Renovate Helm Chart

This Helm chart deploys the Start Renovate application (Renovate configuration generator) to a Kubernetes cluster.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- Sealed Secrets controller installed in your cluster
- Container registry with your images
- OpenAI API key
- A reachable PostgreSQL database (for the dashboard's per-user repo selection)
- A GitHub OAuth App (for the dashboard login) with callback URL
  `https://renovate.oglimmer.com/api/login/oauth2/code/github`

## Components

- **Backend**: Spring Boot application (Java 21) serving the API under `/api`
- **Frontend**: Nuxt.js application serving the web interface
- **Dashboard**: GitHub-authenticated `/dashboard` page that compares Renovate configuration
  across a user's repositories. Login uses GitHub OAuth (servlet Spring Security); the per-user
  set of tracked repositories is persisted in PostgreSQL (Flyway-managed schema)
- **Ingress**: Routes `/api/*` to backend and `/*` to frontend
- **Rate limiting**: A Traefik `RateLimit` middleware on a dedicated, higher-priority
  Ingress limits `/api/feedback` to 1 request/minute per client IP

## Rate limiting

The `/api/feedback` endpoint is rate limited at the edge by Traefik (no app-level
state, survives restarts, works per real client IP). It is implemented as:

- `templates/middleware-ratelimit.yaml` — a Traefik `RateLimit` Middleware CRD
- `templates/ingress-feedback.yaml` — a separate Ingress with an `Exact` match on
  `/api/feedback` that references the middleware. The exact path outranks the main
  Ingress's `/api` prefix, so Traefik routes matching requests through the limiter.

Tunable under `ingress.rateLimit` in `values.yaml` (`average`, `period`, `burst`,
`ipStrategyDepth`). Set `ipStrategyDepth: 1` if another L7 proxy/LB sits in front
of Traefik and populates `X-Forwarded-For`. Requires the Traefik CRDs
(`traefik.io/v1alpha1`) to be installed in the cluster.

## CORS

The backend allows cross-origin requests only from the production origin
(`https://renovate.oglimmer.com`). Override per-environment with the
`APP_CORS_ALLOWED_ORIGIN` env var on the backend (e.g. `http://localhost:3000`).

## Installation

### 1. Create the Sealed Secret for OpenAI API Key

First, you need to create a sealed secret for your OpenAI API key:

```bash
# Create a temporary secret file
cat <<EOF > /tmp/openai-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: start-renovate-openai-secret
  namespace: default
type: Opaque
stringData:
  openai.api-key: "sk-your-actual-openai-api-key-here"
EOF

# Seal the secret (kubeseal must be configured to talk to your cluster)
kubeseal --format yaml < /tmp/openai-secret.yaml > helm/sealed-openai-secret.yaml

# Clean up the temporary file
rm /tmp/openai-secret.yaml

# Apply the sealed secret to your cluster
kubectl apply -f helm/sealed-openai-secret.yaml
```

### 1a. Provision the Postgres role and database (dashboard)

The dashboard persists each user's tracked-repo selection in the cluster's shared
Postgres (`groundhog2k/postgres`, reachable as `postgres.default.svc:5432`). Following
the per-app convention in that chart's repo (role `<app>-app`, database `<app>_prod`,
app role **owns** its DB so Flyway can create tables under PG 15+), create them once as
the superuser:

```bash
# Pick a strong password for the app role; you'll put it in the sealed secret below.
APP_DB_PASS='choose-a-strong-password'

kubectl exec -i postgres-0 -- psql -U postgres -v ON_ERROR_STOP=1 <<SQL
CREATE ROLE "start-renovate-app" WITH LOGIN PASSWORD '${APP_DB_PASS}';
CREATE DATABASE start_renovate_prod OWNER "start-renovate-app";
SQL
```

The app uses `spring.flyway` (migrations under `db/migration`) to create its schema and
`jpa.ddl-auto=validate` to verify it, so no manual table creation is needed.

### 1b. Create the Sealed Secret for GitHub OAuth + Postgres (dashboard)

The dashboard needs GitHub OAuth credentials and the Postgres connection, delivered via the
`start-renovate-app-secret` sealed secret. Use the same `APP_DB_PASS` from step 1a. Follow
`helm/sealed-app-secret.yaml.template`:

```bash
cat <<EOF > /tmp/app-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: start-renovate-app-secret
  namespace: default
type: Opaque
stringData:
  github.client-id: "Iv1.xxxxxxxxxxxx"
  github.client-secret: "your-github-oauth-client-secret"
  db.url: "jdbc:postgresql://postgres.default.svc:5432/start_renovate_prod"
  db.username: "start-renovate-app"
  db.password: "${APP_DB_PASS}"
EOF

kubeseal --format yaml < /tmp/app-secret.yaml > helm/sealed-app-secret.yaml
rm /tmp/app-secret.yaml
kubectl apply -f helm/sealed-app-secret.yaml
```

The GitHub OAuth App's **Authorization callback URL** must be
`https://renovate.oglimmer.com/api/login/oauth2/code/github`. The requested scopes
(`repo`, `read:org`) are configured in the backend, not the secret.

### 2. Update values.yaml

Edit `start-renovate/values.yaml` to customize:
- Image repository and tags
- Resource limits
- Ingress hostname and TLS settings
- Image pull secrets

### 3. Install the Chart

```bash
# From the helm directory
helm install start-renovate ./start-renovate

# Or with custom values
helm install start-renovate ./start-renovate -f my-values.yaml
```

### 4. Upgrade the Chart

```bash
helm upgrade start-renovate ./start-renovate
```

### 5. Uninstall the Chart

```bash
helm uninstall start-renovate
```

## Configuration

The following table lists the configurable parameters:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `backend.image.repository` | Backend image repository | `registry.oglimmer.com/start-renovate-be` |
| `backend.image.tag` | Backend image tag | `latest` |
| `backend.resources.limits.memory` | Backend memory limit | `1Gi` |
| `backend.resources.requests.memory` | Backend memory request | `512Mi` |
| `frontend.image.repository` | Frontend image repository | `registry.oglimmer.com/start-renovate-fe` |
| `frontend.image.tag` | Frontend image tag | `latest` |
| `ingress.enabled` | Enable ingress | `true` |
| `ingress.hosts[0].host` | Hostname | `renovate.oglimmer.com` |
| `ingress.rateLimit.enabled` | Enable Traefik rate limiting on the feedback endpoint | `true` |
| `ingress.rateLimit.path` | Path to rate limit | `/api/feedback` |
| `ingress.rateLimit.average` | Allowed requests per period | `1` |
| `ingress.rateLimit.period` | Rate limit window | `1m` |
| `ingress.rateLimit.burst` | Burst capacity | `1` |
| `ingress.rateLimit.ipStrategyDepth` | X-Forwarded-For depth for client IP | `0` |

## Security

- Runs as non-root user (UID 1000)
- Drops all capabilities
- Uses read-only root filesystem where possible
- OpenAI API key stored as sealed secret

## Monitoring

The backend includes Spring Boot Actuator endpoints:
- Health check: `/api/actuator/health`
- Metrics: `/api/actuator/metrics` (if enabled)

## Troubleshooting

### Check pod status
```bash
kubectl get pods -l app.kubernetes.io/name=start-renovate
```

### View logs
```bash
# Backend logs
kubectl logs -l app.kubernetes.io/component=backend

# Frontend logs
kubectl logs -l app.kubernetes.io/component=frontend
```

### Check sealed secret
```bash
kubectl get sealedsecret start-renovate-openai-secret
kubectl get secret start-renovate-openai-secret
```

### Port forward for local testing
```bash
# Backend
kubectl port-forward svc/start-renovate-backend 8080:8080

# Frontend
kubectl port-forward svc/start-renovate-frontend 8081:80
```
