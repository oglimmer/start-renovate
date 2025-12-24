# Start Renovate Helm Chart

This Helm chart deploys the Start Renovate application (Renovate configuration generator) to a Kubernetes cluster.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- Sealed Secrets controller installed in your cluster
- Container registry with your images
- OpenAI API key

## Components

- **Backend**: Spring Boot application (Java 21) serving the API under `/api`
- **Frontend**: Nuxt.js application serving the web interface
- **Ingress**: Routes `/api/*` to backend and `/*` to frontend

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
