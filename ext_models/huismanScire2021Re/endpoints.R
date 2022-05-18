source("estimate_Re.R")

#* @param data The estimated cases through time
#* @post /get-re
function(data) {
  data_cleaned <- tibble(data) %>%
    mutate(date = as.Date(date))
  result <- calculate(data_cleaned)
  return(result)
}

# data <- read_csv(paste0(data_dir, '/estimated_case_numbers_viollier.csv'))
# result <- calculate(data)
# write_csv(result, paste0('./figures/Re.csv'))
