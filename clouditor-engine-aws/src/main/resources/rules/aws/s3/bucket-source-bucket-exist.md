# Delete replicated bucket without source bucket

Replicated Buckets should have corresponding source buckets.

```ccl
Bucket with (not empty listBucketResult) has (replicatedObject == true)
```

[comment] Bucket has (replicatedObject == true) in any listBucketResult