# Configuration

The following environment variables are required:

Name | Description | Example |
|---|---|---|
| COV_SPECTRUM_DB_HOST | The host URL to the database server | 127.0.0.1 |
| COV_SPECTRUM_DB_PORT | The port that the database server is listening to | 5432 |
| COV_SPECTRUM_DB_NAME | The database name | cov_spectrum |
| COV_SPECTRUM_DB_USERNAME | The database username | cov_spectrum |
| COV_SPECTRUM_DB_PASSWORD | The database password | secret |
| COV_SPECTRUM_JWT_TOKEN_LIFETIME_SECONDS | The length of time for which a JWT token is valid | 259200 |
| COV_SPECTRUM_JWT_SECRET | The secret used to sign the JWT tokens | secret |
