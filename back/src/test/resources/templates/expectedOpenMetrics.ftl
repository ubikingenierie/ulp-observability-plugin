<#ftl  encoding="UTF-8">
<#setting locale="en_US">
<#-- should add <#rt>${'\n'} at the end of lines otherwise will consider the '\r\n' as break line -->
# TYPE ${sampleName}_pct summary<#rt>${'\n'}
# UNIT ${sampleName}_pct milliseconds<#rt>${'\n'}
# HELP ${sampleName}_pct Response percentiles<#rt>${'\n'}
${sampleName}_pct{quantile="${quantile1}"} ${pct1}<#rt>${'\n'}
${sampleName}_pct{quantile="${quantile2}"} ${pct2}<#rt>${'\n'}
${sampleName}_pct{quantile="${quantile3}"} ${pct3}<#rt>${'\n'}
${sampleName}_pct{quantile_every_periods="${quantile1}"} ${pctTotal1}<#rt>${'\n'}
${sampleName}_pct{quantile_every_periods="${quantile2}"} ${pctTotal2}<#rt>${'\n'}
${sampleName}_pct{quantile_every_periods="${quantile3}"} ${pctTotal3}<#rt>${'\n'}
${sampleName}_pct_sum ${sum}<#rt>${'\n'}
${sampleName}_pct_created ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_max gauge<#rt>${'\n'}
# UNIT ${sampleName}_max milliseconds<#rt>${'\n'}
# HELP ${sampleName}_max Max response times<#rt>${'\n'}
${sampleName}_max ${max} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_max_every_periods gauge<#rt>${'\n'}
# UNIT ${sampleName}_max_every_periods milliseconds<#rt>${'\n'}
# HELP ${sampleName}_max_every_periods Total max response times<#rt>${'\n'}
${sampleName}_max_every_periods ${maxTotal} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_avg gauge<#rt>${'\n'}
# UNIT ${sampleName}_avg milliseconds<#rt>${'\n'}
# HELP ${sampleName}_avg Average response times<#rt>${'\n'}
${sampleName}_avg ${avg?string["0.0#####"]} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_avg_every_periods gauge<#rt>${'\n'}
# UNIT ${sampleName}_avg_every_periods milliseconds<#rt>${'\n'}
# HELP ${sampleName}_avg_every_periods Total average response times<#rt>${'\n'}
${sampleName}_avg_every_periods ${avgTotal?string["0.0#####"]} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_total gauge<#rt>${'\n'}
# HELP ${sampleName}_total Response count<#rt>${'\n'}
${sampleName}_total{count="sampler_count_every_periods"} ${samplerCountTotal} ${timestamp}<#rt>${'\n'}
${sampleName}_total{count="sampler_count"} ${samplerCount} ${timestamp}<#rt>${'\n'}
${sampleName}_total{count="error"} ${error} ${timestamp}<#rt>${'\n'}
${sampleName}_total{count="error_every_periods"} ${errorTotal} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_throughput gauge<#rt>${'\n'}
# HELP ${sampleName}_throughput Responses per second<#rt>${'\n'}
${sampleName}_throughput ${throughput?string["0.0#####"]} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_throughput_every_periods gauge<#rt>${'\n'}
# HELP ${sampleName}_throughput_every_periods Total responses per second<#rt>${'\n'}
${sampleName}_throughput_every_periods ${throughputTotal?string["0.0#####"]} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_threads counter<#rt>${'\n'}
# HELP ${sampleName}_threads Current period Virtual user count<#rt>${'\n'}
${sampleName}_threads ${threads} ${timestamp}<#rt>${'\n'}
# TYPE ${sampleName}_threads_every_periods counter<#rt>${'\n'}
# HELP ${sampleName}_threads_every_periods Max number of virtual user count<#rt>${'\n'}
${sampleName}_threads_every_periods ${threadsTotal} ${timestamp}<#rt>${'\n'}
