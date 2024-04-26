#!/bin/bash

num_tests=1000
num_parallel_clients=1
csv_file="response_times_1clients.csv"

# Create CSV file with headers
echo "response" > "$csv_file"

# Function to perform a single test for a specific client
perform_test() {
    local test_number=$1
    local client_number=$2
    local csv_file=$3
    local response

    response=$(curl --silent --header "content-type: text/xml" -d @requestaddorder.xml -w "%{time_total}\n" -o /dev/null  http://dsgtn.uaenorth.cloudapp.azure.com:8082/ws)
    if [ -z "$response" ]; then
        echo "Error: Empty response received for Test $test_number, Client $client_number."
    else
        echo "Test $test_number complete for Client $client_number. Response time: $response seconds"
        echo "$response" >> "$csv_file"
    fi
}

# Loop to run tests in parallel for each client
for ((i=1; i<=num_tests; i++))
do
    for ((j=1; j<=num_parallel_clients; j++))
    do
        perform_test "$i" "$j" "$csv_file" &
    done
    wait
done


echo "All tests complete. Results saved to $csv_file"
