# Find new interesting variants

Example usage for Euler:

```
# Load python module
env2lmod
module load python/3.7.4

# Build Singularity image
singularity build cov-spectrum-server.sif docker://ghcr.io/cevo-public/cov-spectrum-server:develop

# Write environments variables (required for the database) to cov-env.txt
...

# Submit job Switzerland:
bsub -n 1 -R "rusage[mem=6000]" -W 4:00 "singularity exec --env-file cov-env.txt cov-spectrum-server.sif java -Xmx5g -jar /app/cov-spectrum.jar --find-interesting-variants \"Switzerland\" 0.005 5 2000"

# Submit jobs for all countries:
python euler.py
```
