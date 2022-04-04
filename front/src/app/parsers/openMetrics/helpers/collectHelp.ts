import UlpObservabilityMetric from "src/app/models/Metric";

const collectHelp = ( line: String) => {

  // Check if HELP is present
  const match = line.match(/^# HELP ([^\s]+) (.*)$/);

  // If not, skip
  if (!match || !match.length)
    return {};

  // Set name and help properties
  return {name : match[1], help : match[2]}
};


export  {collectHelp};