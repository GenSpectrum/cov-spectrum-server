import argparse
from dataclasses import dataclass
import psycopg
import requests
from psycopg.conninfo import make_conninfo


def create_db_connection_factory(db_host, db_port, db_name, db_user, db_password):
    def inner():
        return psycopg.connect(make_conninfo(
            host=db_host,
            port=db_port,
            dbname=db_name,
            user=db_user,
            password=db_password
        ))
    return inner


def send_notification(auth_key, subject, body):
    url = 'https://dev.cov-spectrum.org/notification/send'
    requests.post(url, json={
        'auth_key': auth_key,
        'channel': 'cs-ingest-default',
        'level': 'INFO',
        'subject': subject,
        'body': body
    })


def fetch_remote_aliases():
    """Fetch data from the official repository and parse the JSON"""
    url = 'https://raw.githubusercontent.com/cov-lineages/pango-designation/master/pango_designation/alias_key.json'
    all_aliases = requests.get(url).json()
    filtered = {}
    for alias, full_name in all_aliases.items():
        # We will ignore recombinants and the root lineages A and B which are mapped to an empty string
        if isinstance(full_name, str) and full_name != '':
            filtered[alias] = full_name
    return filtered


def fetch_our_known_aliases(db_factory):
    """Fetch data from our database"""
    aliases = {}
    with db_factory() as conn:
        cur = conn.cursor()
        cur.execute('select alias, full_name from pango_lineage_alias;')
        for row in cur:
            aliases[row[0]] = row[1]
    return aliases


@dataclass
class ChangeSet:
    to_add: list[tuple[str, str]]
    to_update: list[tuple[str, str]]
    to_delete: list[tuple[str, str]]


def detect_changes(remote, local) -> ChangeSet:
    to_add = []
    to_update = []
    to_delete = []
    for x in remote.items():
        alias, full_name = x
        if alias not in local:
            to_add.append(x)
        elif local[alias] != full_name:
            to_update.append(x)
    for x in local.items():
        alias, _ = x
        if alias not in remote:
            to_delete.append(x)
    return ChangeSet(to_add, to_update, to_delete)


def write_to_db(db_factory, aliases):
    with db_factory() as conn:
        cur = conn.cursor()
        # Delete existing values
        cur.execute('delete from pango_lineage_alias;')
        # Insert
        values = list(aliases.items())
        cur.executemany('''
            insert into pango_lineage_alias (alias, full_name)
            values (%s, %s);
        ''', values)


def format_aliases(aliases):
    return ', '.join(['{}={}'.format(x[0], x[1]) for x in aliases])


def main():
    # parse args
    parser = argparse.ArgumentParser()
    parser.add_argument('--db-host', required=True)
    parser.add_argument('--db-port', required=True)
    parser.add_argument('--db-name', required=True)
    parser.add_argument('--db-user', required=True)
    parser.add_argument('--db-password', required=True)
    parser.add_argument('--notification-key', required=True)
    args = parser.parse_args()

    db_factory = create_db_connection_factory(args.db_host, args.db_port, args.db_name, args.db_user, args.db_password)
    aliases_remote = fetch_remote_aliases()
    aliases_local = fetch_our_known_aliases(db_factory)
    change_set = detect_changes(aliases_remote, aliases_local)
    if len(change_set.to_add) > 0 or len(change_set.to_update) > 0 or len(change_set.to_delete) > 0:
        print('Found changes: {}'.format(change_set))
        write_to_db(db_factory, aliases_remote)
        send_notification(
            args.notification_key,
            '[pangoLineageAliases] Updates',
            'Added: {}\nUpdated: {}\nDeleted: {}'.format(
                format_aliases(change_set.to_add),
                format_aliases(change_set.to_update),
                format_aliases(change_set.to_delete)
            )
        )


if __name__ == '__main__':
    main()
