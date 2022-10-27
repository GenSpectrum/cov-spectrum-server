import argparse
import psycopg
import csv
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


def empty_str_to_none(s):
    return s if s != '' else None


def str_to_int(s):
    if s is None:
        return None
    return int(float(s))


def fetch_owid_data():
    url = 'https://covid.ourworldindata.org/data/owid-covid-data.csv'
    r = requests.get(url, stream=True)
    return csv.DictReader(r.iter_lines(decode_unicode=True), delimiter=',')


def write_to_db(db_factory, reader):
    # The statements will be executed in one transaction by default:
    # https://www.psycopg.org/psycopg3/docs/basic/transactions.html
    with db_factory() as conn:
        cur = conn.cursor()
        # Delete existing values
        cur.execute('delete from cases_raw_owid;')
        # Insert
        values = []
        for row in reader:
            if row['iso_code'] == '' or row['continent'] == '' or row['location'] == '' or row['date'] == '':
                continue
            values.append((
                empty_str_to_none(row['iso_code']),
                empty_str_to_none(row['continent']),
                empty_str_to_none(row['location']),
                empty_str_to_none(row['date']),
                empty_str_to_none(row['new_cases_per_million']),
                empty_str_to_none(row['new_deaths_per_million']),
                str_to_int(empty_str_to_none(row['new_cases'])),
                str_to_int(empty_str_to_none(row['new_deaths']))
            ))
        cur.executemany("""
            insert into cases_raw_owid (
              iso_country, region, country, date, new_cases_per_million,
              new_deaths_per_million, new_cases, new_deaths
            )
            values (%s, %s, %s, %s, %s, %s, %s, %s);
        """, values)


def main():
    # parse args
    parser = argparse.ArgumentParser()
    parser.add_argument('--db-host', required=True)
    parser.add_argument('--db-port', required=True)
    parser.add_argument('--db-name', required=True)
    parser.add_argument('--db-user', required=True)
    parser.add_argument('--db-password', required=True)
    args = parser.parse_args()

    db_factory = create_db_connection_factory(args.db_host, args.db_port, args.db_name, args.db_user, args.db_password)
    reader = fetch_owid_data()
    write_to_db(db_factory, reader)


if __name__ == '__main__':
    main()
