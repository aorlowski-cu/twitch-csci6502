SELECT
  COUNT(user_name) AS stream_count,
  SUM(stream_length_hours) AS stream_time,
  AVG(stream_length_hours) AS average_length,
  AVG(streamers.followerCount) as followers,
  AVG(avg_viewer_count) as aggAvgViewerCount,
  user_name
FROM (
  SELECT
    user_name,
    user_id,
    started_at,
    COUNT(id)*15/60 AS stream_length_hours,
    AVG(viewer_count) AS avg_viewer_count
  FROM
    `molten-castle-305417.twitch_streams.snapshot`
  GROUP BY
    started_at,
    user_name,
    user_id)
INNER JOIN
  `molten-castle-305417.twitch_streams.streamers` streamers
ON
  streamers.id = user_id
GROUP BY
  user_name
ORDER BY
  stream_time DESC




WITH data AS (
    SELECT * FROM `molten-castle-305417.twitch_streams.streamers`
  )
  , quantiles AS (
  SELECT *, IFNULL(LEAD(bucket_start) OVER(ORDER BY bucket_i) , 0100000) bucket_end
  FROM UNNEST((
    SELECT APPROX_QUANTILES(followerCount, 10)
    FROM data
  )) bucket_start WITH OFFSET bucket_i
)

SELECT COUNT(*) count, bucket_i
  , ANY_VALUE(STRUCT(bucket_start, bucket_end)) b, MIN(followerCount) min,  MAX(followerCount) max
FROM data
JOIN quantiles
ON data.followerCount >= bucket_start AND data.followerCount < bucket_end
GROUP BY bucket_i
ORDER BY bucket_i
