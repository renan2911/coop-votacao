## API de Votação de Cooperativa

API REST para gerenciamento de pautas, sessões de votação e votos de cooperados.

### Stack

- Java 21
- Spring Boot 3
- MySQL (persistência)
- Redis (contadores de votos e cache de CPF)
- Kafka (publicação de resultado de votação)
- Docker (containerização)

### Principais endpoints (v1)

- `POST /api/v1/pautas` – cria pauta
- `GET /api/v1/pautas` – lista pautas
- `POST /api/v1/pautas/{pautaId}/sessoes` – abre sessão (default 60s)
- `POST /api/v1/pautas/{pautaId}/votos` – registra voto (`SIM`/`NAO`) para um CPF
- `GET /api/v1/pautas/{pautaId}/resultado` – obtém resumo da votação

Documentação OpenAPI/Swagger disponível em `/swagger-ui.html` após subir a aplicação.

### Requisitos externos

- MySQL escutando em `localhost:3306`, database `coop_votacao`, usuário `root`/`root` (ajuste em `application.yml` se necessário).
- Redis em `localhost:6379`.
- Kafka em `localhost:9092` e tópico `pauta-resultados` (configurável em `application.yml`).

### Execução

```bash
mvn spring-boot:run
```

#### Método 2: Docker Compose (Recomendado)

```bash
# Inicia todos os serviços (MySQL, Redis, Kafka, App)
docker-compose up -d

### Containerização

O projeto está containerizad seguindo:

- ✅ Dockerfile multi-stage
- ✅ Docker Compose com todos os serviços

### Decisões Arquiteturais

**Por que não Apache Avro?**
O evento de resultado contém apenas 7 campos primitivos. O overhead de configurar Avro + Schema Registry não se justifica para mensagens pequenas (~200 bytes). A diferença de performance entre JSON e Avro é negligível neste volume.

- Swagger: http://localhost:8080/swagger-ui.html
- Kafka UI: http://localhost:8090

### Testes

```bash
mvn test
```

### Estratégia de performance

- Contadores de votos por sessão em Redis (operações atômicas `INCR`).
- Uso de constraint única `(pauta_id, associado_id)` para garantir um voto por pauta/associado.
- Cache em Redis para status de CPF retornado pela API externa.

                  
                  
    