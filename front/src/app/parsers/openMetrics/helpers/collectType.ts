import UlpObservabilityMetric from "src/app/models/Metric";

const collectType = (line: String) => {

  // Check if type is present
  const match = line.match(/^# TYPE ([^\s]+) (.*)$/);

  // If not, skip
  if (!match || !match.length)
    return '';

  // return type
  return match[2];

};


export {collectType};