#!/bin/bash

# Generate SSL certificate for HTTPS
echo "🔐 Generating SSL certificate for development..."

# Create SSL directory if it doesn't exist
mkdir -p src/main/resources/ssl

# Generate keystore with self-signed certificate
keytool -genkey -alias backend     -storetype PKCS12     -keyalg RSA     -keysize 2048     -keystore src/main/resources/ssl/keystore.p12     -validity 3650     -storepass changeit     -keypass changeit     -dname "CN=localhost,OU=Development,O=Backend,L=City,ST=State,C=US"

echo "✅ SSL certificate generated successfully!"
echo "📁 Certificate location: src/main/resources/ssl/keystore.p12"
echo "🔑 Keystore password: changeit"
echo "🔑 Key alias: backend"
echo ""
echo "⚠️  This is a self-signed certificate for development only!"
echo "🌐 For production, use a certificate from a trusted CA."
echo ""
echo "To enable HTTPS, set in application.yml:"
echo "server:"
echo "  ssl:"
echo "    enabled: true"
echo "    key-store: classpath:ssl/keystore.p12"
echo "    key-store-password: changeit"
echo "    key-store-type: PKCS12"
echo "    key-alias: backend"
