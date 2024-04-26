#!/bin/bash

# Number of times to test
num_tests=1

# Array of DNS names to test
dns_names=("wout.uksouth" "dsgtn.uaenorth" "dsgt.westus2" "dsgt1.japaneast")

# Header for the CSV file
header="Test Number"
for dns_name in "${dns_names[@]}"
do
    header="$header,$dns_name"
done

  echo "Running test"

  # Array to store response statuses for each DNS name
  response_statuses=()

  # Loop through each DNS name
  for dns_name in "${dns_names[@]}"
  do
      # Run curl command and capture response size
      response_size=$(curl --silent --header "content-type: text/xml" -d @requestaddorder.xml -w "%{size_download}\n" -o /dev/null "http://$dns_name.cloudapp.azure.com:8082/ws"
)

      # Check if response size is 465
      if [ "$response_size" == "465" ]; then
          status="OK"
      else
          status="Incorrect Size"
      fi

      # Add response status to array
      response_statuses+=("$status")

      # Echo the response status
      echo "Test - $dns_name: $status"
  done


echo "All tests complete."
