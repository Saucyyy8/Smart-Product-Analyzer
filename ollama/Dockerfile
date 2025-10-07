# Start from the base ollama image
FROM ollama/ollama:latest

# Copy the entrypoint script into the container
COPY entrypoint.sh /entrypoint.sh

# Make the script executable
RUN chmod +x /entrypoint.sh

# Set the entrypoint script as the command to run when the container starts
ENTRYPOINT ["/entrypoint.sh"]
