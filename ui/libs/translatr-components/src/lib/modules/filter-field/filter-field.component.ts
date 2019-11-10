import { ChangeDetectionStrategy, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FilterFieldFilter, FilterFieldSelection } from '@translatr/translatr-components/src';
import { FormControl } from '@angular/forms';
import { MatAutocompleteTrigger, MatOptionSelectionChange, ThemePalette } from '@angular/material';

const lowerCaseIncludes = (s: string, search: string): boolean =>
  s.toLowerCase().includes(search.toLowerCase());

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-filter-field',
  templateUrl: './filter-field.component.html',
  styleUrls: ['./filter-field.component.scss']
})
export class FilterFieldComponent implements OnInit {
  @Input() filters: ReadonlyArray<FilterFieldFilter>;
  @Input() appearance: 'standard' | 'outline' = 'standard';
  @Input() color: ThemePalette = 'primary';
  @Output() selected = new EventEmitter<ReadonlyArray<FilterFieldSelection>>();
  @ViewChild('autocompleteInput', { static: false }) autocompleteInput: ElementRef;
  @ViewChild(MatAutocompleteTrigger, { static: false }) autocompleteTrigger: MatAutocompleteTrigger;
  filterControl = new FormControl('');
  autocompleteOptions: Array<FilterFieldFilter> = [];

  private _selection: ReadonlyArray<FilterFieldFilter>;

  get selection(): ReadonlyArray<FilterFieldFilter> {
    return this._selection;
  }

  @Input() set selection(selection: ReadonlyArray<FilterFieldFilter>) {
    console.log('selection', selection);
    this._selection = selection;
    this._options = selection;
  }

  private _options: ReadonlyArray<FilterFieldFilter> = [];

  get options(): ReadonlyArray<FilterFieldFilter> {
    return this._options;
  }

  set options(options: ReadonlyArray<FilterFieldFilter>) {
    this._options = options;
    console.log('emitting', options);
    this.selected.emit(options);
  }

  ngOnInit() {
    this.filterControl.valueChanges
      .subscribe((value: string | FilterFieldFilter) => {
        console.log('valueChange', value);
        if (typeof value === 'string') {
          this.updateAutocompleteOptions({
            ...this.filters.find(f => f.key === 'search'),
            key: 'search',
            value
          });
        } else {
          this.updateAutocompleteOptions(value);
        }
      });
    this.updateAutocompleteOptions();
  }

  displayFn(option) {
    return option ? option.title : option;
  }

  onRemoved(option: FilterFieldSelection) {
    console.log('removed', option);
    this.removeOption(option.key);
    if (option.key === 'search') {
      this.filterControl.setValue('');
    }
  }

  onAutocompleteSelected(selection: MatOptionSelectionChange) {
    console.log('autocompleteSelected', selection);
    this.autocompleteInput.nativeElement.focus();
    this.autocompleteInput.nativeElement.value = '';
    this.filterControl.setValue('');
    this.updateOption(selection.source.value);
  }

  private updateOption(option: FilterFieldFilter): void {
    console.log('updateOption', option);
    this.options = [
      ...this.options.filter(o => o.key !== option.key),
      option
    ];
    this.updateAutocompleteOptions();
  }

  private removeOption(key: string): void {
    console.log('removeOption', key, this.options);
    this.options = this.options.filter(o => o.key !== key);
    this.updateAutocompleteOptions();
  }

  private updateAutocompleteOptions(option?: FilterFieldFilter): void {
    console.log('updateAutocompleteOptions', option);
    const booleans = this.options
      .filter(s => s.type === 'boolean')
      .map(s => s.key);
    const filters = this.filters
      .filter(f => !booleans.includes(f.key));

    if (option !== undefined) {
      this.autocompleteOptions = filters
        .filter(o =>
          (option.value !== '' && !o.allowEmpty)
          || (option.value === '' && o.allowEmpty)
          || (option.value !== '' && lowerCaseIncludes(o.title, option.value.toString())))
        .filter(o => o.type !== 'number' || !isNaN(option.value))
        .map((o) => ({
            ...o,
            value: o.type !== 'boolean' ? option.value : o.value
          })
        );
    } else {
      this.autocompleteOptions = filters
        .filter(o => o.allowEmpty);
    }
  }
}
