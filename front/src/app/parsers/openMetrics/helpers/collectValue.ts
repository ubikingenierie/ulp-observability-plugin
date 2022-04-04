import UlpObservabilityMetric from "src/app/models/Metric";


const collectValue = (line: String) => {

  // Check if value is present
  const match = line.match(/^(([^\s]+)({[^\s]*})|([^\s]+)) ((.*) (.*)|(.*))$/i);
  let value : any;

  // If not, skip
  if(!match || !match.length) return;

  // Process if there is a timestamp (1) or not (2)
  if(match[6] && match[7])
    value = { value: parseFloat(match[6]), timestamp: parseFloat(match[7]) };
  else
    value = { value: parseFloat(match[5]) };

  // Check if label is present
  if(match[3])
    value.labels = JSON.parse(match[3].replace(/([^{}=,]+)[,]?=/g, '"$1":'));

  return value;
};

export {collectValue};