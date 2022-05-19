FROM rocker/tidyverse:4
WORKDIR /app/

RUN apt-get update
RUN apt-get install -y git

RUN git clone https://github.com/covid-19-Re/shiny-dailyRe.git

COPY install_packages.R .
RUN Rscript install_packages.R
COPY . .

EXPOSE 7080
ENTRYPOINT ["Rscript", "app.R"]
