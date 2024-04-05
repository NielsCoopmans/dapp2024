#!/bin/bash

# Number of times to test
num_tests=100

# CSV file to save the results
csv_file="response_times.csv"

# Header for the CSV file
echo "Test Number,Response Time (s)" > "$csv_file"

# Array to store response times
response_times=()

# Loop for each test
for ((i=1; i<=$num_tests; i++))
do
    echo "Running test $i..."

    # Run curl command and capture response time
    response=$(curl --silent --header "content-type: text/xml" -d @requestaddorder.xml -w "%{time_total}\n" -o /dev/null http://dsgt.westus2.cloudapp.azure.com:8082/ws)

    # Append result to CSV file
    echo "$i,$response" >> "$csv_file"

    # Add response time to array
    response_times+=("$response")

    echo "Test $i complete. Response time: $response seconds"
done

# Calculate average response time
total_time=0
for time in "${response_times[@]}"
do
    total_time=$(echo "$total_time + $time" | bc)
done
average_time=$(echo "scale=2; $total_time / $num_tests" | bc)

echo "Average Response Time: $average_time seconds"

echo "All tests complete. Results saved to $csv_file"
