#!/bin/bash

# Number of times to test
num_tests=100

# Array of DNS names to test
dns_names=("wout.uksouth" "dsgtn.uaenorth" "dsgt.westus2" "dsgt1.japaneast")

# CSV file to save the results
csv_file="response_times_belgium.csv"

# Header for the CSV file
header="Test Number"
for dns_name in "${dns_names[@]}"
do
    header="$header,$dns_name"
done
echo "$header" > "$csv_file"

# Loop for each test
for ((i=1; i<=$num_tests; i++))
do
    echo "Running test $i..."

    # Array to store response times for each DNS name
    response_times=()

    # Loop through each DNS name
    for dns_name in "${dns_names[@]}"
    do
        # Run curl command and capture response time
        response=$(curl --silent --header "content-type: text/xml" -d @requestaddorder.xml -w "%{time_total}\n" -o /dev/null "http://$dns_name.cloudapp.azure.com:8082/ws")
        # Add response time to array
        response_times+=("$response")
    done

    # Construct row for the CSV file
    row="$i"
    for response_time in "${response_times[@]}"
    do
        row="$row,$response_time"
    done

    # Append result row to CSV file
    echo "$row" >> "$csv_file"

    echo "Test $i complete."
done

echo "All tests complete. Results saved to $csv_file"