#!/bin/bash

num_tests=2000

csv_file="response_times.csv"

echo "Response Time (s)" > "$csv_file"


for ((i=1; i<=$num_tests; i++))
do
    echo "Running test $i..."

    response=$(curl -X GET --silent -w "%{time_total}\n" -o /dev/null http://dsgtn.uaenorth.cloudapp.azure.com:8084/restrpc/meals)

    echo "$response" >> "$csv_file"


    echo "Test $i complete. Response time: $response seconds"
done

echo "All tests complete. Results saved to $csv_file"
