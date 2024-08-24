export interface TextNode {
    id: string;
    content: string;
  }
  
  export interface ImageNode {
    id: string;
    imagePath: string;
  }
  
  export interface NoteChild {
    id: string;
    position: number;
    textNode?: TextNode;
    imageNode?: ImageNode;
    type: 'text' | 'image';
  }
  
  export interface Note {
    id: string;
    title: string;
    contents: NoteChild[];
    lastInteractedWith: string;
  }
  