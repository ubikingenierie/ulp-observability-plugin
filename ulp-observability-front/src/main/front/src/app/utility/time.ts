export function seconds(s: number): number {
    return s*1000;
  }

export function minutes(m: number): number {
    return seconds(m*60);
}