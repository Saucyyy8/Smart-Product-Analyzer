import { useState } from "react";
import { Search, Link, Type, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card } from "@/components/ui/card";

interface SearchInterfaceProps {
  onAnalyze: (input: string, type: 'url' | 'description') => void;
  isAnalyzing: boolean;
}

const SearchInterface = ({ onAnalyze, isAnalyzing }: SearchInterfaceProps) => {
  const [urlInput, setUrlInput] = useState("");
  const [descriptionInput, setDescriptionInput] = useState("");
  const [activeTab, setActiveTab] = useState("url");

  const handleSubmit = () => {
    const input = activeTab === "url" ? urlInput : descriptionInput;
    if (input.trim()) {
      onAnalyze(input, activeTab as 'url' | 'description');
    }
  };

  const isValid = activeTab === "url" ? urlInput.trim().length > 0 : descriptionInput.trim().length > 0;

  return (
    <section id="search-section" className="py-20 bg-muted/30">
      <div className="container mx-auto px-4">
        <div className="max-w-2xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              Start Your Product Analysis
            </h2>
            <p className="text-lg text-muted-foreground">
              Enter a product URL from any retailer or describe what you're looking for
            </p>
          </div>

          <Card className="result-card">
            <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
              <TabsList className="grid w-full grid-cols-2 mb-6">
                <TabsTrigger value="url" className="flex items-center gap-2">
                  <Link className="w-4 h-4" />
                  Product URL
                </TabsTrigger>
                <TabsTrigger value="description" className="flex items-center gap-2">
                  <Type className="w-4 h-4" />
                  Description
                </TabsTrigger>
              </TabsList>

              <TabsContent value="url" className="space-y-4">
                <div>
                  <label htmlFor="url-input" className="text-sm font-medium text-foreground mb-2 block">
                    Product URL
                  </label>
                  <Input
                    id="url-input"
                    type="url"
                    placeholder="https://amazon.com/product-link or any retailer URL"
                    value={urlInput}
                    onChange={(e) => setUrlInput(e.target.value)}
                    className="text-base"
                    disabled={isAnalyzing}
                  />
                  <p className="text-sm text-muted-foreground mt-2">
                    Paste any product URL from Amazon, eBay, or other retailers
                  </p>
                </div>
              </TabsContent>

              <TabsContent value="description" className="space-y-4">
                <div>
                  <label htmlFor="description-input" className="text-sm font-medium text-foreground mb-2 block">
                    Product Description
                  </label>
                  <Textarea
                    id="description-input"
                    placeholder="Describe the product you're looking for... e.g., 'wireless noise-canceling headphones under $200 with good battery life'"
                    value={descriptionInput}
                    onChange={(e) => setDescriptionInput(e.target.value)}
                    className="min-h-24 text-base resize-none"
                    disabled={isAnalyzing}
                  />
                  <p className="text-sm text-muted-foreground mt-2">
                    Be specific about features, budget, and preferences for better results
                  </p>
                </div>
              </TabsContent>

              <div className="flex justify-center pt-4">
                <Button 
                  variant="hero" 
                  size="lg" 
                  onClick={handleSubmit}
                  disabled={!isValid || isAnalyzing}
                  className="w-full sm:w-auto min-w-48"
                >
                  {isAnalyzing ? (
                    <>
                      <Loader2 className="w-5 h-5 mr-2 animate-spin" />
                      Analyzing Products...
                    </>
                  ) : (
                    <>
                      <Search className="w-5 h-5 mr-2" />
                      Analyze Products
                    </>
                  )}
                </Button>
              </div>
            </Tabs>
          </Card>

          {isAnalyzing && (
            <Card className="result-card mt-8">
              <div className="text-center py-8">
                <div className="w-16 h-16 bg-gradient-to-r from-primary to-accent rounded-full flex items-center justify-center mx-auto mb-4 glow-primary">
                  <Loader2 className="w-8 h-8 text-primary-foreground animate-spin" />
                </div>
                <h3 className="text-xl font-semibold mb-2">Analyzing Products</h3>
                <p className="text-muted-foreground mb-4">
                  Searching Amazon, analyzing reviews, and comparing features...
                </p>
                <div className="max-w-md mx-auto">
                  <div className="flex justify-between text-sm text-muted-foreground mb-2">
                    <span>Progress</span>
                    <span>Analyzing...</span>
                  </div>
                  <div className="w-full bg-muted rounded-full h-2">
                    <div className="bg-gradient-to-r from-primary to-accent h-2 rounded-full animate-pulse" style={{ width: '75%' }} />
                  </div>
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>
    </section>
  );
};

export default SearchInterface;