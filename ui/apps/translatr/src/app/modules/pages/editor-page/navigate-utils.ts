import { combineLatest, Observable } from 'rxjs';
import { PagedList } from '@dev/translatr-model';
import { MessageItem } from './message-item';
import { take } from 'rxjs/operators';

export const navigateItems = (
  messageItems$: Observable<PagedList<MessageItem>>,
  selected$: Observable<string>,
  matcher: (messageItem: MessageItem, selected: string) => boolean,
  defaultIndex: (listLength: number) => number,
  nextIndex: (index: number | undefined) => number
): Promise<MessageItem> => {
  return new Promise((resolve, reject) => {
    combineLatest([messageItems$, selected$])
      .pipe(take(1))
      .subscribe(([messageItems, selected]) => {
        console.log('messageItems, selected', messageItems, selected);
        if (messageItems === undefined || messageItems.list === undefined || messageItems.list.length === 0) {
          return reject();
        }
        if (selected === undefined) {
          const i = defaultIndex(messageItems.list.length);
          console.log('selecting default in list', messageItems.list[i]);
          return resolve(messageItems.list[i]);
        }
        const index = nextIndex(messageItems.list.findIndex(messageItem => matcher(messageItem, selected)));
        console.log('selecting next in list', index);
        if (index !== undefined && index >= 0 && index < messageItems.list.length) {
          return resolve(messageItems.list[index]);
        }

        reject();
      });
  });
};
