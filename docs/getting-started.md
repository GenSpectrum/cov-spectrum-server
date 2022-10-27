# Getting Started

The simplest way to start a (local) instance of the CoV-Spectrum server is using Docker and Docker-Compose:

1. Create an empty PostgreSQL database
2. Fill out the empty values in [docker-compose.yml](../docker-compose.yml): the database user should preferably be the owner of the database
3. Run `docker compose up`

The program will automatically initiate the database.
