export class DynamicFlatNode {
  constructor(
    public item: any,
    public level = 1,
    public expandable = false,
    public isLoading = false
  ) {}
}
