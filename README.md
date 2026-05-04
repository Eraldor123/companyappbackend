Pro instalaci a spuštění docker container:
docker run --hostname=6ca72261c41a --env=PG_MAJOR=18 --env=PG_VERSION=18.2-1.pgdg13+1 --env=PGDATA=/var/lib/postgresql/18/docker --env=POSTGRES_PASSWORD=heslo123 --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/postgresql/18/bin --env=GOSU_VERSION=1.19 --env=LANG=en_US.utf8 --volume=/var/lib/postgresql --network=bridge -p 5432:5432 --restart=no --runtime=runc -d postgres

Nastavit v project settings Java 21.

poté spustit.
