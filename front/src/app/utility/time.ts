export function seconds(s: number): number {
    return s*1000;
  }

export function minutes(m: number): number {
    return seconds(m*60);
}

export function hours(h: number): number {
    return minutes(h*60);
}

export function days(d: number): number {
    return hours(d*24);
}