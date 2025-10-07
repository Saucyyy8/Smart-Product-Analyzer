#!/bin/sh
# This script runs when the container starts.

# 1. Start the Ollama server in the background.
ollama serve &

# 2. Wait for the server to be ready.
# A simple sleep is often enough for local setups.
sleep 5

# 3. Pull the model.
echo "Pulling llama3 model..."
ollama pull llama3
echo "Model pull complete."

# 4. Bring the server process to the foreground to keep the container running.
wait
