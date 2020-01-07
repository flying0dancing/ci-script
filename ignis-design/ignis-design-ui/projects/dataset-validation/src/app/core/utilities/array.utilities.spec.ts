import * as ArrayUtilities from './array.utilities';
import { flatten, max, min } from './array.utilities';

describe('ArrayUtilities', () => {
  describe('isArray', () => {
    it('should return true if the provided argument is an array', () => {
      expect(ArrayUtilities.isArray([])).toEqual(true);
      expect(ArrayUtilities.isArray(['A'])).toEqual(true);
    });

    it('should return false if the provided argument is not an array', () => {
      expect(ArrayUtilities.isArray(null)).toEqual(false);
      expect(ArrayUtilities.isArray(undefined)).toEqual(false);
      expect((<any>ArrayUtilities).isArray(1)).toEqual(false);
      expect((<any>ArrayUtilities).isArray('A')).toEqual(false);
    });
  });

  describe('flatten', () => {
    it('should flatten nested arrays', () => {
      const array = [
        0,
        1,
        2,
        [3],
        [4, 5],
        [6, [7, 8, 9]],
        [10, [11, [12, 13]]]
      ];

      const flattened = flatten(array);

      expect(flattened).toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]);
    });
  });
  describe('max', () => {
    it('should return max value using extractor', () => {
      const array = [
        { x: 'one', y: 1 },
        { x: 'two', y: 2 },
        { x: 'four', y: 4 },
        { x: 'three', y: 3 }
      ];

      const maxVal = max<any, number>(array, t => t.y);

      expect(maxVal).toEqual({ x: 'four', y: 4 });
    });

    it('should return first max value when there are duplicates', () => {
      const array = [
        { x: 'one', y: 1 },
        { x: 'two', y: 2 },
        { x: 'four#1', y: 4 },
        { x: 'three', y: 3 },
        { x: 'four#2', y: 4 }
      ];

      const maxVal = max<any, number>(array, t => t.y);

      expect(maxVal).toEqual({ x: 'four#1', y: 4 });
    });
  });

  describe('min', () => {
    it('should return min value using extractor', () => {
      const array = [
        { x: 'one', y: 1 },
        { x: 'two', y: 2 },
        { x: 'four', y: 4 },
        { x: 'three', y: 3 }
      ];

      const minVal = min<any, number>(array, t => t.y);

      expect(minVal).toEqual({ x: 'one', y: 1 });
    });

    it('should return first min value when there are duplicates', () => {
      const array = [
        { x: 'two', y: 2 },
        { x: 'one#1', y: 1 },
        { x: 'four', y: 4 },
        { x: 'one#2', y: 1 },
        { x: 'three', y: 3 }
      ];

      const minVal = min<any, number>(array, t => t.y);

      expect(minVal).toEqual({ x: 'one#1', y: 1 });
    });
  });

  describe('moveElement', () => {
    it('should shift all elements above up an index when index to is first index', () => {
      const values = [1, 2, 3, 4, 5, 6, 7, 8, 9];

      const newArr = ArrayUtilities.moveElement(4, 0, values);

      expect(newArr).toEqual([5, 1, 2, 3, 4, 6, 7, 8, 9]);
    });

    it('should with simple change', () => {
      const values = [1, 2, 3];

      const newArr = ArrayUtilities.moveElement(0, 1, values);

      expect(newArr).toEqual([2, 1, 3]);
    });

    it('should with simple change to end', () => {
      const values = [1, 2, 3];

      const newArr = ArrayUtilities.moveElement(0, 2, values);

      expect(newArr).toEqual([2, 3, 1]);
    });

    it('should with simple change reversed', () => {
      const values = [1, 2, 3];

      const newArr = ArrayUtilities.moveElement(1, 0, values);

      expect(newArr).toEqual([2, 1, 3]);
    });

    it('should with simple change to start', () => {
      const values = [1, 2, 3];

      const newArr = ArrayUtilities.moveElement(2, 0, values);

      expect(newArr).toEqual([3, 1, 2]);
    });

    it('should shift all elements above up an index when index to is middle', () => {
      const values = [1, 2, 3, 4, 5, 6, 7, 8, 9];

      const newArr = ArrayUtilities.moveElement(4, 2, values);

      expect(newArr).toEqual([1, 2, 5, 3, 4, 6, 7, 8, 9]);
    });

    it('should shift all elements down up an index when item is moved later in list', () => {
      const values = [1, 2, 3, 4, 5, 6, 7, 8, 9];

      const newArr = ArrayUtilities.moveElement(2, 4, values);

      expect(newArr).toEqual([1, 2, 4, 5, 3, 6, 7, 8, 9]);
    });

    it('should shift all elements down up an index when item is moved last in list', () => {
      const values = [1, 2, 3, 4, 5, 6, 7, 8, 9];

      const newArr = ArrayUtilities.moveElement(2, 8, values);

      expect(newArr).toEqual([1, 2, 4, 5, 6, 7, 8, 9, 3]);
    });
  });
});
