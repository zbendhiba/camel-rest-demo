mkdir -p target
cat s3Tolog.yaml | envsubst > target/s3Tolog.yaml
kubectl apply -f target/s3Tolog.yaml
rm target/s3Tolog.yaml

