import { useState } from "react";
import { useToast } from "@/hooks/use-toast";
import SearchInterface from "@/components/SearchInterface";
import ResultsDisplay from "@/components/ResultsDisplay";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";

const AnalysisPage = () => {
    const [searchResults, setSearchResults] = useState(null);
    const [isAnalyzing, setIsAnalyzing] = useState(false);
    const { toast } = useToast();
    const { token, logout } = useAuth();
    const navigate = useNavigate();

    const handleAnalysis = async (input: string, type: 'url' | 'description') => {
        setIsAnalyzing(true);
        setSearchResults(null); // Clear previous results immediately

        const headers: HeadersInit = {
            'Content-Type': 'application/json',
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            // NEW: Streaming Implementation
            // We use standard fetch but read the stream body
            const response = await fetch('http://localhost:8080/product/stream', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    input: input,
                    type: type
                })
            });

            if (!response.ok) {
                if (response.status === 401) logout();
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // Create a reader to read the stream
            const reader = response.body?.getReader();
            const decoder = new TextDecoder();

            if (!reader) throw new Error("No readable stream available");

            let done = false;
            let receivedProducts: any[] = [];

            // Initialize empty results container immediately if you want, 
            // or wait for first product.
            // Let's set initial structure so UI can render if we have empty lists
            // But usually we wait for first product to set SearchResults

            let buffer = '';

            while (!done) {
                const { value, done: streamDone } = await reader.read();
                done = streamDone;

                if (value) {
                    const chunk = decoder.decode(value, { stream: true });
                    console.log("Received chunk:", chunk); // Debug logging
                    buffer += chunk;
                    const lines = buffer.split('\n');

                    // Keep the last line in the buffer as it might be incomplete
                    // If the chunk ended with \n, the last line will be empty, which is fine
                    buffer = lines.pop() || '';

                    for (const line of lines) {
                        const trimmedLine = line.trim();
                        if (!trimmedLine) continue;

                        if (trimmedLine.startsWith('data:')) {
                            try {
                                const jsonStr = trimmedLine.replace('data:', '').trim();
                                if (!jsonStr) continue;
                                const product = JSON.parse(jsonStr);

                                console.log("Parsed Product from Stream:", product.name); // Debug log

                                receivedProducts.push(product);

                                // Format the product immediately
                                const formattedProduct = {
                                    id: receivedProducts.length,
                                    name: product.name,
                                    price: product.price || "N/A",
                                    rating: product.rating || 0,
                                    pros: product.pros || [],
                                    cons: product.cons || [],
                                    score: (product.rating || 0) * 10,
                                    isRecommended: product.recommended ?? (receivedProducts.length === 1), // Use backend field if available
                                    verdict: product.verdict,
                                    url: product.url,
                                    imageUrl: product.imageUrl
                                };

                                // Update State incrementally
                                setSearchResults((prev: any) => {
                                    if (!prev) {
                                        return {
                                            query: input,
                                            products: [formattedProduct]
                                        };
                                    }
                                    // Avoid duplicates if SSE sends duplicate by mistake
                                    if (prev.products.some((p: any) => p.name === formattedProduct.name)) {
                                        return prev;
                                    }
                                    return {
                                        ...prev,
                                        products: [...prev.products, formattedProduct]
                                    };
                                });

                            } catch (e) {
                                console.error("Error parsing SSE data", e);
                            }
                        }
                    }
                }
            }

            toast({
                title: "Analysis Complete!",
                description: "All products have been analyzed.",
            });

        } catch (error) {
            console.error('Analysis failed:', error);
            if (!(error instanceof Error && error.message.includes('401'))) {
                toast({
                    title: "Analysis Failed",
                    description: "Stream connection lost or failed.",
                    variant: "destructive",
                });
            }
        } finally {
            setIsAnalyzing(false);
        }
    };

    return (
        <div className="min-h-screen bg-background p-6">
            <div className="max-w-7xl mx-auto mb-8">
                <Button variant="ghost" className="mb-4" onClick={() => navigate("/")}>
                    <ArrowLeft className="w-4 h-4 mr-2" /> Back to Home
                </Button>
                <h1 className="text-3xl font-bold">Product Analysis</h1>
                <p className="text-muted-foreground">Enter a product URL or description to get started.</p>
            </div>

            <SearchInterface onAnalyze={handleAnalysis} isAnalyzing={isAnalyzing} />
            {searchResults && (
                <ResultsDisplay results={searchResults} />
            )}
        </div>
    );
};

export default AnalysisPage;
