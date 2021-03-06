export interface DateStringTemporal {
  whenCreated?: Date | string;
  whenUpdated?: Date | string;
}

export const convertTemporals = <T extends DateStringTemporal>(t: T): T => {
  if (!t) {
    return t;
  }

  return {
    ...(t as object),
    whenCreated: t.whenCreated && new Date(t.whenCreated),
    whenUpdated: t.whenUpdated && new Date(t.whenUpdated)
  } as T;
};

export const convertTemporalsList = <T extends DateStringTemporal>(list?: T[]): T[] | undefined => {
  if (!list) {
    return list;
  }

  return list.map(convertTemporals);
};
