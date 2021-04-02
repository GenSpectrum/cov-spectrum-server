import json
import os

min_number_samples_relative = "0.005"
max_variant_length = "5"
max_number_of_variants = "2000"

bsub_mem = {
    "S": "1000",
    "M": "3000",
    "L": "6000",
    "XL": "20000",
    "XXL": "70000",
    "XXXL": "160000"
}

bsub_time = {
    "S": "0:30",
    "M": "1:00",
    "L": "4:00",
    "XL": "4:00",
    "XXL": "10:00",
    "XXXL": "10:00"
}

java_mem = {
    "S": "800m",
    "M": "2500m",
    "L": "5g",
    "XL": "18g",
    "XXL": "65g",
    "XXXL": "135g"
}

with open("countries.json", "r") as f:
    countries = json.load(f)

for country in countries:
    name = country["name"]
    resource_category = country["resourceCategory"]
    command = ("bsub -n 1 -R \"rusage[mem={}]\" "
               "-W {} \"singularity exec --env-file cov-env.txt cov-spectrum-server.sif "
               "java -Xmx{} -jar /app/cov-spectrum.jar --find-interesting-variants \\\"{}\\\" {} {} {}\""
               "".format(bsub_mem[resource_category],
                         bsub_time[resource_category],
                         java_mem[resource_category],
                         name,
                         min_number_samples_relative,
                         max_variant_length,
                         max_number_of_variants
                         ))
    os.system(command)
