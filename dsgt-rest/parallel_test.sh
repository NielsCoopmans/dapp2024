#!/bin/bash

num_tests=1000
num_parallel_clients=30
csv_file="response_times.csv"

# Create CSV file with headers for each client
echo "response" > "$csv_file"


# Initialize array to store response times for each client
declare -A response_times

# Function to perform a single test for a specific client
perform_test() {
    local test_number=$1
    local client_number=$2
    local csv_file=$3
    local response

    response=$(curl -X GET --silent -w "%{time_total}\n" -o /dev/null http://dsgtn.uaenorth.cloudapp.azure.com:8084/restrpc/meals)
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
