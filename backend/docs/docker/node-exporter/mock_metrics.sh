#!/usr/bin/env bash

set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="${DIR}/test.prom"
TMP="${DIR}/.test.prom.tmp"
STATE="${DIR}/.mock_state"

INTERVAL="${1:-5}"

APP="order-service"
INSTANCE="order-1"
POOL="HikariPool-1"

JVM_EDEN_MAX=536870912
JVM_OLD_MAX=1073741824
JVM_META_MAX=268435456

if [[ ! -f "${STATE}" ]]; then
  cat > "${STATE}" <<'EOF'
HTTP_POST_200=1200
HTTP_POST_400=20
HTTP_POST_500=6
HTTP_GET_200=3600
HTTP_GET_404=50
HTTP_PAY_200=950
HTTP_PAY_502=9

HTTP_POST_200_SUM=210.4
HTTP_POST_400_SUM=3.2
HTTP_POST_500_SUM=9.8
HTTP_GET_200_SUM=280.1
HTTP_GET_404_SUM=4.6
HTTP_PAY_200_SUM=190.7
HTTP_PAY_502_SUM=5.1

GC_MINOR=80
GC_MAJOR=2
GC_MINOR_SUM=3.2
GC_MAJOR_SUM=1.1
EOF
fi

# shellcheck disable=SC1090
source "${STATE}"

rand_int() {
  local min="$1" max="$2"
  echo $(( min + RANDOM % (max - min + 1) ))
}

rand_float3() {
  local min="$1" max="$2"
  awk -v min="$min" -v max="$max" 'BEGIN{srand(); printf "%.3f", min + (max-min)*rand()}'
}

write_prom() {
  cat > "${TMP}" <<EOF
# HELP http_server_requests_seconds_count HTTP request count
# TYPE http_server_requests_seconds_count counter
http_server_requests_seconds_count{application="${APP}",method="POST",uri="/api/orders",status="200",instance="${INSTANCE}"} ${HTTP_POST_200}
http_server_requests_seconds_count{application="${APP}",method="POST",uri="/api/orders",status="400",instance="${INSTANCE}"} ${HTTP_POST_400}
http_server_requests_seconds_count{application="${APP}",method="POST",uri="/api/orders",status="500",instance="${INSTANCE}"} ${HTTP_POST_500}
http_server_requests_seconds_count{application="${APP}",method="GET",uri="/api/orders/{id}",status="200",instance="${INSTANCE}"} ${HTTP_GET_200}
http_server_requests_seconds_count{application="${APP}",method="GET",uri="/api/orders/{id}",status="404",instance="${INSTANCE}"} ${HTTP_GET_404}
http_server_requests_seconds_count{application="${APP}",method="POST",uri="/api/payments",status="200",instance="${INSTANCE}"} ${HTTP_PAY_200}
http_server_requests_seconds_count{application="${APP}",method="POST",uri="/api/payments",status="502",instance="${INSTANCE}"} ${HTTP_PAY_502}

# HELP http_server_requests_seconds_sum HTTP request duration sum
# TYPE http_server_requests_seconds_sum counter
http_server_requests_seconds_sum{application="${APP}",method="POST",uri="/api/orders",status="200",instance="${INSTANCE}"} ${HTTP_POST_200_SUM}
http_server_requests_seconds_sum{application="${APP}",method="POST",uri="/api/orders",status="400",instance="${INSTANCE}"} ${HTTP_POST_400_SUM}
http_server_requests_seconds_sum{application="${APP}",method="POST",uri="/api/orders",status="500",instance="${INSTANCE}"} ${HTTP_POST_500_SUM}
http_server_requests_seconds_sum{application="${APP}",method="GET",uri="/api/orders/{id}",status="200",instance="${INSTANCE}"} ${HTTP_GET_200_SUM}
http_server_requests_seconds_sum{application="${APP}",method="GET",uri="/api/orders/{id}",status="404",instance="${INSTANCE}"} ${HTTP_GET_404_SUM}
http_server_requests_seconds_sum{application="${APP}",method="POST",uri="/api/payments",status="200",instance="${INSTANCE}"} ${HTTP_PAY_200_SUM}
http_server_requests_seconds_sum{application="${APP}",method="POST",uri="/api/payments",status="502",instance="${INSTANCE}"} ${HTTP_PAY_502_SUM}

# HELP http_server_requests_active In-flight requests
# TYPE http_server_requests_active gauge
http_server_requests_active{application="${APP}",instance="${INSTANCE}"} ${HTTP_ACTIVE}

# HELP jvm_memory_max_bytes JVM memory max
# TYPE jvm_memory_max_bytes gauge
jvm_memory_max_bytes{application="${APP}",instance="${INSTANCE}",area="heap",id="G1 Eden Space"} ${JVM_EDEN_MAX}
jvm_memory_max_bytes{application="${APP}",instance="${INSTANCE}",area="heap",id="G1 Old Gen"} ${JVM_OLD_MAX}
jvm_memory_max_bytes{application="${APP}",instance="${INSTANCE}",area="nonheap",id="Metaspace"} ${JVM_META_MAX}

# HELP jvm_memory_used_bytes JVM memory used
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="${APP}",instance="${INSTANCE}",area="heap",id="G1 Eden Space"} ${JVM_EDEN_USED}
jvm_memory_used_bytes{application="${APP}",instance="${INSTANCE}",area="heap",id="G1 Old Gen"} ${JVM_OLD_USED}
jvm_memory_used_bytes{application="${APP}",instance="${INSTANCE}",area="nonheap",id="Metaspace"} ${JVM_META_USED}

# HELP jvm_memory_committed_bytes JVM memory committed
# TYPE jvm_memory_committed_bytes gauge
jvm_memory_committed_bytes{application="${APP}",instance="${INSTANCE}",area="heap",id="G1 Eden Space"} ${JVM_EDEN_COMMITTED}
jvm_memory_committed_bytes{application="${APP}",instance="${INSTANCE}",area="heap",id="G1 Old Gen"} ${JVM_OLD_COMMITTED}
jvm_memory_committed_bytes{application="${APP}",instance="${INSTANCE}",area="nonheap",id="Metaspace"} ${JVM_META_COMMITTED}

# HELP jvm_threads_daemon_threads JVM daemon threads
# TYPE jvm_threads_daemon_threads gauge
jvm_threads_daemon_threads{application="${APP}",instance="${INSTANCE}"} ${JVM_THREADS_DAEMON}

# HELP jvm_threads_live_threads JVM live threads
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads{application="${APP}",instance="${INSTANCE}"} ${JVM_THREADS_LIVE}

# HELP jvm_threads_peak_threads JVM peak threads
# TYPE jvm_threads_peak_threads gauge
jvm_threads_peak_threads{application="${APP}",instance="${INSTANCE}"} ${JVM_THREADS_PEAK}

# HELP jvm_gc_pause_seconds_count JVM GC pause count
# TYPE jvm_gc_pause_seconds_count counter
jvm_gc_pause_seconds_count{application="${APP}",instance="${INSTANCE}",action="end of minor GC",cause="G1 Evacuation Pause"} ${GC_MINOR}
jvm_gc_pause_seconds_count{application="${APP}",instance="${INSTANCE}",action="end of major GC",cause="G1 Compaction Pause"} ${GC_MAJOR}

# HELP jvm_gc_pause_seconds_sum JVM GC pause sum
# TYPE jvm_gc_pause_seconds_sum counter
jvm_gc_pause_seconds_sum{application="${APP}",instance="${INSTANCE}",action="end of minor GC",cause="G1 Evacuation Pause"} ${GC_MINOR_SUM}
jvm_gc_pause_seconds_sum{application="${APP}",instance="${INSTANCE}",action="end of major GC",cause="G1 Compaction Pause"} ${GC_MAJOR_SUM}

# HELP hikaricp_connections HikariCP connections
# TYPE hikaricp_connections gauge
hikaricp_connections{application="${APP}",pool="${POOL}",instance="${INSTANCE}"} ${HIKARI_CONN}

# HELP up Target up
# TYPE up gauge
up{job="${APP}"} 1

# HELP app_build_info Build info
# TYPE app_build_info gauge
app_build_info{application="${APP}",version="1.2.3",commit="a1b2c3d"} 1
EOF

  printf '\n' >> "${TMP}"
  mv "${TMP}" "${OUT}"
}

while true; do
  inc_orders_ok="$(rand_int 0 4)"
  inc_orders_fail="$(rand_int 0 1)"

  inc_post_500=0
  if (( RANDOM % 25 == 0 )); then
    inc_post_500=1
  fi

  inc_get_200=$(( (inc_orders_ok + inc_orders_fail) * $(rand_int 1 5) ))
  inc_get_404=0
  if (( RANDOM % 10 == 0 )); then
    inc_get_404=1
  fi

  inc_pay_ok="$(rand_int 0 4)"
  inc_pay_fail=0
  if (( RANDOM % 30 == 0 )); then
    inc_pay_fail=1
  fi

  HTTP_POST_200=$((HTTP_POST_200 + inc_orders_ok))
  HTTP_POST_400=$((HTTP_POST_400 + inc_orders_fail))
  HTTP_POST_500=$((HTTP_POST_500 + inc_post_500))
  HTTP_GET_200=$((HTTP_GET_200 + inc_get_200))
  HTTP_GET_404=$((HTTP_GET_404 + inc_get_404))
  HTTP_PAY_200=$((HTTP_PAY_200 + inc_pay_ok))
  HTTP_PAY_502=$((HTTP_PAY_502 + inc_pay_fail))

  HTTP_POST_200_SUM="$(awk -v a="${HTTP_POST_200_SUM}" -v b="$(rand_float3 0.080 0.350)" -v c="${inc_orders_ok}" 'BEGIN{printf "%.3f", a + b*c}')"
  HTTP_POST_400_SUM="$(awk -v a="${HTTP_POST_400_SUM}" -v b="$(rand_float3 0.060 0.200)" -v c="${inc_orders_fail}" 'BEGIN{printf "%.3f", a + b*c}')"
  HTTP_POST_500_SUM="$(awk -v a="${HTTP_POST_500_SUM}" -v b="$(rand_float3 0.200 0.800)" -v c="${inc_post_500}" 'BEGIN{printf "%.3f", a + b*c}')"
  HTTP_GET_200_SUM="$(awk -v a="${HTTP_GET_200_SUM}" -v b="$(rand_float3 0.010 0.090)" -v c="${inc_get_200}" 'BEGIN{printf "%.3f", a + b*c}')"
  HTTP_GET_404_SUM="$(awk -v a="${HTTP_GET_404_SUM}" -v b="$(rand_float3 0.010 0.080)" -v c="${inc_get_404}" 'BEGIN{printf "%.3f", a + b*c}')"
  HTTP_PAY_200_SUM="$(awk -v a="${HTTP_PAY_200_SUM}" -v b="$(rand_float3 0.050 0.600)" -v c="${inc_pay_ok}" 'BEGIN{printf "%.3f", a + b*c}')"
  HTTP_PAY_502_SUM="$(awk -v a="${HTTP_PAY_502_SUM}" -v b="$(rand_float3 0.200 0.900)" -v c="${inc_pay_fail}" 'BEGIN{printf "%.3f", a + b*c}')"

  HTTP_ACTIVE="$(rand_int 0 25)"
  HIKARI_CONN=$((12 + $(rand_int -4 8)))
  if (( HIKARI_CONN < 0 )); then HIKARI_CONN=0; fi

  JVM_EDEN_COMMITTED=$((JVM_EDEN_MAX - $(rand_int 80000000 180000000)))
  JVM_EDEN_USED=$((JVM_EDEN_COMMITTED - $(rand_int 20000000 70000000)))
  if (( JVM_EDEN_USED < 0 )); then JVM_EDEN_USED=0; fi

  JVM_OLD_COMMITTED=$((JVM_OLD_MAX - $(rand_int 150000000 300000000)))
  JVM_OLD_USED=$((JVM_OLD_COMMITTED - $(rand_int 30000000 120000000)))
  if (( JVM_OLD_USED < 0 )); then JVM_OLD_USED=0; fi

  JVM_META_COMMITTED=$((JVM_META_MAX - $(rand_int 20000000 50000000)))
  JVM_META_USED=$((JVM_META_COMMITTED - $(rand_int 5000000 15000000)))
  if (( JVM_META_USED < 0 )); then JVM_META_USED=0; fi

  JVM_THREADS_DAEMON=$((35 + $(rand_int -5 10)))
  JVM_THREADS_LIVE=$((70 + $(rand_int -10 25)))
  JVM_THREADS_PEAK=$((JVM_THREADS_LIVE + $(rand_int 0 10)))

  if (( RANDOM % 5 == 0 )); then
    GC_MINOR=$((GC_MINOR + 1))
    GC_MINOR_SUM="$(awk -v a="${GC_MINOR_SUM}" -v b="$(rand_float3 0.005 0.080)" 'BEGIN{printf "%.3f", a + b}')"
  fi
  if (( RANDOM % 5 == 0 )); then
    GC_MAJOR=$((GC_MAJOR + 1))
    GC_MAJOR_SUM="$(awk -v a="${GC_MAJOR_SUM}" -v b="$(rand_float3 0.100 0.800)" 'BEGIN{printf "%.3f", a + b}')"
  fi

  write_prom

  cat > "${STATE}" <<EOF
HTTP_POST_200=${HTTP_POST_200}
HTTP_POST_400=${HTTP_POST_400}
HTTP_POST_500=${HTTP_POST_500}
HTTP_GET_200=${HTTP_GET_200}
HTTP_GET_404=${HTTP_GET_404}
HTTP_PAY_200=${HTTP_PAY_200}
HTTP_PAY_502=${HTTP_PAY_502}

HTTP_POST_200_SUM=${HTTP_POST_200_SUM}
HTTP_POST_400_SUM=${HTTP_POST_400_SUM}
HTTP_POST_500_SUM=${HTTP_POST_500_SUM}
HTTP_GET_200_SUM=${HTTP_GET_200_SUM}
HTTP_GET_404_SUM=${HTTP_GET_404_SUM}
HTTP_PAY_200_SUM=${HTTP_PAY_200_SUM}
HTTP_PAY_502_SUM=${HTTP_PAY_502_SUM}

GC_MINOR=${GC_MINOR}
GC_MAJOR=${GC_MAJOR}
GC_MINOR_SUM=${GC_MINOR_SUM}
GC_MAJOR_SUM=${GC_MAJOR_SUM}
EOF

  sleep "${INTERVAL}"
done
